package trace4cats.datadog

import java.math.BigInteger
import java.util.concurrent.TimeUnit

import cats.Foldable
import cats.syntax.foldable._
import cats.syntax.show._
import io.circe.Encoder
import io.circe.generic.semiauto._
import trace4cats.SemanticTags
import trace4cats.model.{AttributeValue, Batch}

// implements https://docs.datadoghq.com/tracing/guide/send_traces_to_agent_by_api/
case class DataDogSpan(
  trace_id: BigInteger,
  span_id: BigInteger,
  parent_id: Option[BigInteger],
  name: String,
  service: String,
  resource: String,
  meta: Map[String, String],
  metrics: Map[String, Double],
  start: Long,
  duration: Long,
  error: Option[Int],
  `type`: String
)

object DataDogSpan {
  def fromBatch[F[_]: Foldable](batch: Batch[F]): List[List[DataDogSpan]] =
    batch.spans.toList
      .groupBy(_.context.traceId)
      .values
      .toList
      .map(_.map { span =>
        // IDs use BigIntegers so that they can be unsigned
        val traceId = new BigInteger(1, span.context.traceId.value.drop(8))
        val spanId = new BigInteger(1, span.context.spanId.value)
        val parentId = span.context.parent.map { parent =>
          new BigInteger(1, parent.spanId.value)
        }

        val allAttributes = span.allAttributes ++ SemanticTags
          .kindTags(span.kind) ++ SemanticTags.statusTags("")(span.status)

        val startNanos = TimeUnit.SECONDS.toNanos(span.start.getEpochSecond) + span.start.getNano
        val endNanos = TimeUnit.SECONDS.toNanos(span.end.getEpochSecond) + span.end.getNano

        DataDogSpan(
          traceId,
          spanId,
          parentId,
          span.name,
          span.serviceName,
          allAttributes.get("resource.name").fold(span.serviceName)(_.toString),
          allAttributes.collect {
            case (k, AttributeValue.StringValue(value)) => k -> value.value
            case (k, AttributeValue.BooleanValue(value)) if k != "error" => k -> value.value.toString
            case (k, value: AttributeValue.AttributeList) => k -> value.show
          },
          allAttributes.collect {
            case (k, AttributeValue.DoubleValue(value)) => k -> value.value
            case (k, AttributeValue.LongValue(value)) => k -> value.value.toDouble
          },
          startNanos,
          endNanos - startNanos,
          allAttributes.get("error").map {
            case AttributeValue.BooleanValue(v) if v.value => 1
            case _ => 0
          },
          allAttributes.get("datadog.span_type").fold("custom")(_.toString),
        )
      })

  implicit val encoder: Encoder[DataDogSpan] = deriveEncoder[DataDogSpan].mapJson(_.dropNullValues)
}
