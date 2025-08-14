import logging

from django.conf import settings
from django.core.mail import EmailMessage
from django.template.loader import render_to_string

logger = logging.getLogger('Tourists_in_HKU')


def send_verification_email(email, code):
    """
    Send verification code email using HTML template
    """
    subject = "Your Verification Code"

    # Render the HTML template with context
    html_content = render_to_string(
        'Tourists_in_HKU/verify.html',
        {
            'verification_code': code,
            # Simple approach to get a name from email
            'user_name': email.split('@')[0],
            'logo_url': 'hku.jpg',
            'hku_img_url': 'hku.jpg',
        }
    )

    from_email = settings.DEFAULT_FROM_EMAIL

    try:
        email_message = EmailMessage(
            subject=subject,
            body=html_content,
            from_email=from_email,
            to=[email]
        )
        email_message.content_subtype = 'html'  # Set as HTML
        email_message.send()
        logger.info(f"验证码邮件已成功发送给 {email}")
        return f"Email successfully sent to {email}"
    except Exception as e:
        logger.error(f"发送验证码邮件失败：{str(e)}")
        return f"Failed to send email: {e}"
