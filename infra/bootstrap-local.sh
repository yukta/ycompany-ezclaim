#!/usr/bin/env bash
set -euo pipefail

AWS_ENDPOINT="http://localhost:4566"
REGION="us-east-1"

DDB_TABLE="claims"
S3_BUCKET="claim-documents"

echo "========================================"
echo " Bootstrapping LocalStack on WSL"
echo "========================================"

# 1. Check Docker availability
if ! docker info >/dev/null 2>&1; then
  echo "ERROR: Docker is not running. Start Docker Desktop."
  exit 1
fi

# 2. Check LocalStack health
echo "Checking LocalStack health..."
HEALTH=$(curl -s ${AWS_ENDPOINT}/_localstack/health || true)
if ! echo "$HEALTH" | grep -q '"s3": "running"'; then
  echo "ERROR: LocalStack S3 is not running."
  echo "$HEALTH"
  
fi
echo "LocalStack is healthy"

# 3. Create S3 bucket (idempotent)
echo "Ensuring S3 bucket exists: ${S3_BUCKET}"
aws --endpoint-url=${AWS_ENDPOINT} \
    --region ${REGION} \
    s3 mb s3://${S3_BUCKET} >/dev/null 2>&1 || true
echo "S3 bucket ready"

# 4. Delete DynamoDB table if exists
echo "Checking DynamoDB table: ${DDB_TABLE}"
if aws --endpoint-url=${AWS_ENDPOINT} \
      --region ${REGION} \
      dynamodb describe-table \
      --table-name ${DDB_TABLE} >/dev/null 2>&1; then
  echo "Deleting DynamoDB table: ${DDB_TABLE}"
  aws --endpoint-url=${AWS_ENDPOINT} \
      --region ${REGION} \
      dynamodb delete-table \
      --table-name ${DDB_TABLE}

  echo "Waiting for table deletion..."
  sleep 5
else
  echo "No existing DynamoDB table found"
fi

# 5. Terraform init + apply
echo "Running Terraform..."

terraform init -reconfigure
terraform apply -auto-approve

aws --endpoint-url=http://localhost:4566 --region us-east-1  s3 mb s3://claim-documents

echo "========================================"
echo " LocalStack bootstrap completed"
echo "========================================"
