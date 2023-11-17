resource "aws_apprunner_service" "service" {
  service_name = var.serv_name

  instance_configuration {
    instance_role_arn = aws_iam_role.role_for_apprunner_service.arn
    cpu = "256"
    memory = "1024"
  }

  source_configuration {
    authentication_configuration {
      access_role_arn = var.arn
    }
    image_repository {
      image_configuration {
        port = "8080"
      }
      image_identifier      = var.img_id
      image_repository_type = "ECR"
    }
    auto_deployments_enabled = true
  }
}

resource "aws_iam_role" "role_for_apprunner_service" {
  name               = var.names[0]
  assume_role_policy = data.aws_iam_policy_document.assume_role.json
}


data "aws_iam_policy_document" "assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["tasks.apprunner.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

data "aws_iam_policy_document" "policy" {
  statement {
    effect    = "Allow"
    actions   = ["rekognition:*"]
    resources = ["*"]
  }
  
  statement  {
    effect    = "Allow"
    actions   = ["s3:*"]
    resources = ["*"]
  }

  statement  {
    effect    = "Allow"
    actions   = ["cloudwatch:*"]
    resources = ["*"]
  }
}

resource "aws_iam_policy" "policy" {
  name        = var.names[1]
  description = "Policy granting neccesary permissions for apprunner to run app"
  policy      = data.aws_iam_policy_document.policy.json
}


resource "aws_iam_role_policy_attachment" "attachment" {
  role       = aws_iam_role.role_for_apprunner_service.name
  policy_arn = aws_iam_policy.policy.arn
}