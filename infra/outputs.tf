output "claims_table" {
  value = aws_dynamodb_table.claims.name
}

output "claim_events_topic" {
  value = aws_sns_topic.claim_events.arn
}

output "fraud_queue" {
  value = aws_sqs_queue.fraud_queue.name
}