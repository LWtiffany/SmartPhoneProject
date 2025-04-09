from django.core.mail import EmailMessage
from django.conf import settings
import logging

logger = logging.getLogger('Tourists_in_HKU')


def send_verification_email(email, code):
    """
    发送验证码邮件（同步）
    """
    subject = "Your Verification Code"
    message = f"Your verification code is: {code}\nThis code is valid for 10 minutes."
    from_email = settings.DEFAULT_FROM_EMAIL

    try:
        email_message = EmailMessage(
            subject=subject,
            body=message,
            from_email=from_email,
            to=[email]
        )
        email_message.content_subtype = 'plain'
        email_message.send()
        logger.info(f"验证码邮件已成功发送给 {email}")
        return f"Email successfully sent to {email}"
    except Exception as e:
        logger.error(f"发送验证码邮件失败：{str(e)}")
        return f"Failed to send email: {e}"
