terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.26.0"
    }
  }
  backend "s3" {
    bucket = "pgr301-2021-terraform-state"
    key    = "candidate2025/apprunner-actions.state"
    region = "eu-north-1"
  }
}