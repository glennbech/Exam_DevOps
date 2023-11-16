variable "serv_name"{
  type = string
  default = "candidate2025"
}
variable "arn"{
  type = string
  sensitive = true
}
variable "img_id"{
  type = string
  sensitive = true
}
variable "names"{
  type = list(string)
  default = ["2025-apprunner-role", "2025-apr-policy"]
}