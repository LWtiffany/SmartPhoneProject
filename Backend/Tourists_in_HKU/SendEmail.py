# SendEmail.py
from django.core.mail import send_mail
from django.conf import settings

def send_success_email(to_email):
    try:
        send_mail(
            '预定成功',  # 邮件标题
            '您的预约已成功完成！',  # 邮件正文
            settings.DEFAULT_FROM_EMAIL,  # 发件人邮件地址
            [to_email],  # 收件人邮件地址
            fail_silently=False,  # 如果发送失败会抛出异常
        )
    except Exception as e:
        # 可以在此记录错误或进一步处理
        print(f"邮件发送失败: {e}")
