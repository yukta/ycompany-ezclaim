resource "aws_dynamodb_table" "audit_logs" {
  name         = "audit_logs"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "auditId"

  attribute {
    name = "auditId"
    type = "S"
  }
}
