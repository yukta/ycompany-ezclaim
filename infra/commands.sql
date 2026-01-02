docker compose up -d

'Check if SNS topic exists '
aws --endpoint-url=http://localhost:4566 --region us-east-1 sns list-topics

'This Is a Known Design Pattern: Progressive Enrichment'

aws --endpoint-url=http://localhost:4566 --region us-east-1  s3 ls
aws --endpoint-url=http://localhost:4566 --region us-east-1  s3 mb s3://claim-documents

'We implemented a claim intake service backed by DynamoDB, published lifecycle events via SNS,
processed them asynchronously using a fraud worker consuming from SQS, and enabled secure document uploads using presigned S3 URLs. 
The design is event-driven, decoupled, and easily extensible for AI-based fraud scoring and document workflows.'

aws --endpoint-url=http://localhost:4566 dynamodb scan table-name audit_logs

aws --endpoint-url=http://localhost:4566 dynamodb describe-table --table-name claims