# DynamoDB – Claims table
resource "aws_dynamodb_table" "claims" {
  name         = "claims"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "claimId"

  attribute {
    name = "claimId"
    type = "S"
  }

  attribute {
    name = "customerId"
    type = "S"
  }

  global_secondary_index {
    name            = "customerId-index"
    hash_key        = "customerId"
    projection_type = "ALL"
  }
}


# SNS – Claim events
resource "aws_sns_topic" "claim_events" {
  name = "claim-events"
}

# SQS – Fraud processing queue
resource "aws_sqs_queue" "fraud_queue" {
  name = "fraud-queue"
}

# SNS → SQS subscription
resource "aws_sns_topic_subscription" "fraud_subscription" {
  topic_arn = aws_sns_topic.claim_events.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.fraud_queue.arn
}

# Allow SNS to publish to SQS
resource "aws_sqs_queue_policy" "fraud_queue_policy" {
  queue_url = aws_sqs_queue.fraud_queue.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = "*"
      Action    = "sqs:SendMessage"
      Resource  = aws_sqs_queue.fraud_queue.arn
      Condition = {
        ArnEquals = {
          "aws:SourceArn" = aws_sns_topic.claim_events.arn
        }
      }
    }]
  })
}
