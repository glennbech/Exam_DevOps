resource "aws_cloudwatch_metric_alarm" "threshold"{
    alarm_name = "${var.alarm}-threshold"
    namespace = var.alarm
    metric_name = "total_PPE_violations.count"
    
    comparison_operator = "GreaterThanThreshold"
    threshold = var.threshold
    evaluation_periods = "2"
    period = "60"
    statistic = "Maximum"
    
    alarm_description = "This alarm goes off should the number of violations exeed 50 within 2 minutes"
    alarm_actions = [aws_sns_topic.user_updates.arn]
}
resource "aws_cloudwatch_metric_alarm" "down"{
    alarm_name = "${var.alarm}-down-alert"
    namespace = var.alarm
    metric_name = "image_scan_duration.avg"
    
    comparison_operator = "GreaterThanThreshold"
    threshold = "10000"
    evaluation_periods = "2"
    period = "60"
    statistic = "Maximum"
    
    alarm_description = "This alarm goes off should the it take longer than 10 seconds to scan the images over 2 minutes"
    alarm_actions = [aws_sns_topic.user_updates.arn]
}

resource "aws_sns_topic" "user_updates" {
  name = "${var.alarm}-alarm-topic"
}

resource "aws_sns_topic_subscription" "user_updates_sqs_target" {
  topic_arn = aws_sns_topic.user_updates.arn
  protocol  = "email"
  endpoint  = var.alarm_email
}

