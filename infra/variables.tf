variable "serv_name"{
  type = string
  default = "candidate2025"
}
variable "arn"{
  type = string
  default = "arn:aws:iam::244530008913:role/service-role/AppRunnerECRAccessRole"
}
variable "img_id"{
  type = string
  default = "244530008913.dkr.ecr.eu-west-1.amazonaws.com/candidate2025_ecr_repo:latest"
}
variable "names"{
  type = list(string)
  default = ["2025-apprunner-role", "2025-apr-policy"]
}