import logging

from celery import shared_task
from django.conf import settings
from django.core.mail import EmailMessage
from django.http import HttpResponse
from django.template.loader import render_to_string

logger = logging.getLogger('Tourists_in_HKU')

# @shared_task


def send_success_email(email, name):
    try:
        to_email = email
        user_name = name

        subject = 'Booking Successful'

        # Render the HTML template with context
        html_content = render_to_string(
            'Tourists_in_HKU/sendemail.html',
            {
                'user_name': user_name,
                'logo_url': settings.LOGO_URL,
                'hku_img_url': settings.HKU_IMG_URL,
                # Additional tour details would be added here
            }
        )

        email = EmailMessage(
            subject,
            html_content,
            settings.DEFAULT_FROM_EMAIL,
            [to_email],
        )
        email.content_subtype = 'html'
        email.send()
        logger.info(f"邮件已成功发送给 {user_name} ({to_email})")
        return f"Email successfully sent to {to_email}"
    except Exception as e:
        logger.error(f"发送邮件失败：{str(e)}")
        return f"Failed to send email: {e}"
