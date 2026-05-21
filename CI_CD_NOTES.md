# CI/CD Notes – Tourism Booking System

## Trigger rule
The workflow runs automatically on every push to the `main` branch.

## Pipeline stages
1. Checkout the repository.
2. Set up Java 21.
3. Run `mvn test`.
4. Build the Docker image.
5. Push the image to Amazon ECR.
6. Render a new ECS task definition using the new image.
7. Deploy the updated task definition to the ECS Fargate service.
8. Run a smoke test against `/actuator/health`.

## Image versioning
Each image is tagged with the Git commit SHA using `${{ github.sha }}`.
The workflow also pushes a `latest` tag for convenience.

Example:
`<ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/tourism-booking:<commit-sha>`

## Secrets policy
Database credentials are not stored in GitHub.
The workflow only redeploys ECS.
Database credentials remain in AWS Secrets Manager and are injected into the ECS task at runtime.

GitHub repository secrets needed:
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `ALB_URL`

## Rollback plan
To roll back, open ECS → Cluster → Service → Deployments / Task definitions, then redeploy a previous working task definition revision.
Example: if revision `tourism-booking-task:8` failed, redeploy `tourism-booking-task:7`.
