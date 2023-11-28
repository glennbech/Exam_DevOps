resource "aws_cloudwatch_dashboard" "main" {
    dashboard_name = var.dash_name
    dashboard_body = jsonencode({
        widgets: [
          {
            type = "metric"
            x = 23,
            y = 6,
            width = 12,
            height = 6,
            properties = {
              metrics = [
                ["${var.cloudwatch_namespace}", "image_scan_duration.count", {"label":"Overall Latency"}],
                ["${var.cloudwatch_namespace}", "image_scan_duration.avg", {"label":"Average Latency"}],
                ["${var.cloudwatch_namespace}", "image_scan_duration.max", {"label":"Max Latency"}],
                ["${var.cloudwatch_namespace}", "image_scan_duration.sum", {"label":"Total Latency"}]
              ]
              period = 300,
              stat = "Maximum",
              region = "eu-west-1"
              title = "Method Latency in ms"
            }
          },
          {
            type = "metric"
            x = 0,
            y = 6,
            width = 12,
            height = 6,
            properties = {
            view = "pie"
              metrics = [
                ["${var.cloudwatch_namespace}", "avg_face.count", {"label":"Face Protection Violations"}],
                ["${var.cloudwatch_namespace}", "avg_head.count", {"label":"Head Protection Violations"}],
                ["${var.cloudwatch_namespace}", "avg_hands.count", {"label":"Hand Protection Violations"}]
              ]
              period = 3600,
              stat = "Average",
              region = "eu-west-1"
              title = "Average Violation per Image in the Last Hour"
            }
          },
          {
            type = "metric"
            x = 0,
            y = 0,
            width = 12,
            height = 6,
            properties = {
            view = "bar"
              metrics = [
                ["${var.cloudwatch_namespace}", "total_PPE_violations.count",{"label":"Overall PPE Violations"}],
                ["${var.cloudwatch_namespace}", "violation_face.count", {"label":"Facial Protection Violations"}],
                ["${var.cloudwatch_namespace}", "violation_head.count", {"label":"Head Protection Violations"}],
                ["${var.cloudwatch_namespace}", "violation_hands.count", {"label":"Hand Protection Violations"}]
              ]
              period = 3600,
              stat = "Maximum",
              region = "eu-west-1"
              title = "Total Violations in the Last Hour"
            }
          },
          {
            type = "metric"
            x = 23,
            y = 0,
            width = 12,
            height = 6,
            properties = {
            view = "bar"
              metrics = [
                ["${var.cloudwatch_namespace}", "people_scanned.count",{"label":"People Scanned"}],
                ["${var.cloudwatch_namespace}", "img_scanned.count", {"label":"Images Scanned"}]
                ]
              period = 3600,
              stat = "Maximum",
              region = "eu-west-1"
              title = "Images and People Scanned in the Last Hour"
            }
          }
        ]
    })
}
module "alarm" {
  source = "./alarm_module"
  alarm_email = var.alarm_email
  alarm = var.candnr
}