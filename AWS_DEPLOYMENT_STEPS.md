# AWS Deployment Steps - Tourism Booking

## 1. Build Docker image locally or in AWS CloudShell
```bash
docker build -t tourism-booking .
```

## 2. Create ECR repository
Repository name: tourism-booking

## 3. Login to ECR
```bash
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com
```

## 4. Tag and push image
```bash
docker tag tourism-booking:latest <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/tourism-booking:latest
docker push <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/tourism-booking:latest
```

## 5. Create RDS MySQL
Database name: tourism_booking
Port: 3306
Use Single-AZ for the project.

## 6. Create Secrets Manager secrets
Create secrets for:
- DB_URL = jdbc:mysql://<RDS-ENDPOINT>:3306/tourism_booking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
- DB_USERNAME
- DB_PASSWORD
- JWT_SECRET

## 7. Create ECS Task Definition
Launch type: Fargate
CPU/Memory: 0.5 vCPU / 1 GB
Container port: 8080
Image: ECR image URI
Add environment/secrets:
- DB_URL from Secrets Manager
- DB_USERNAME from Secrets Manager
- DB_PASSWORD from Secrets Manager
- JWT_SECRET from Secrets Manager
- PORT = 8080
Enable CloudWatch Logs.

## 8. Create ALB + Target Group
ALB listener: HTTP 80
Target group type: IP
Protocol: HTTP
Port: 8080
Health check path: /actuator/health
Expected success code: 200

## 9. Security Groups
ALB SG: inbound HTTP 80 from 0.0.0.0/0
ECS SG: inbound TCP 8080 only from ALB SG
RDS SG: inbound TCP 3306 only from ECS SG

## 10. Create ECS Service
Cluster: tourism-booking-cluster
Launch type: Fargate
Desired tasks: 1
Attach ALB target group.

## 11. Test deployment
Open:
http://<ALB-DNS>/actuator/health
Expected response:
{"status":"UP"}

Also test Swagger:
http://<ALB-DNS>/swagger-ui.html
```
