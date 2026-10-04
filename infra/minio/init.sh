set -e
S3="aws --endpoint-url http://minio:9000"

until $S3 s3api list-buckets >/dev/null 2>&1; do sleep 1; done   # wait for MinIO

$S3 s3api head-bucket --bucket eshopper-media 2>/dev/null \
  || $S3 s3api create-bucket --bucket eshopper-media

$S3 s3api put-bucket-policy --bucket eshopper-media \
  --policy file:///init/public-read-policy.json

$S3 s3api put-bucket-lifecycle-configuration --bucket eshopper-media \
  --lifecycle-configuration file:///init/lifecycle.json

echo "storage ready"
