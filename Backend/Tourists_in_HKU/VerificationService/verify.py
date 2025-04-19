from django.core.mail import EmailMessage
from django.conf import settings
import logging

logger = logging.getLogger('Tourists_in_HKU')


def send_verification_email(email, code):
    """
    发送验证码邮件（HTML 格式）
    """
    subject = "Your Verification Code"
    html_content = f"""
    <html>
        <head>
            <style>
                body {{
                    font-family: Arial, sans-serif;
                    color: #333333;
                }}
                .container {{
                    max-width: 600px;
                    margin: auto;
                    padding: 20px;
                    border: 1px solid #e0e0e0;
                    border-radius: 10px;
                    background-color: #f9f9f9;
                }}
                .code {{
                    font-size: 24px;
                    font-weight: bold;
                    color: #4CAF50;
                    margin: 20px 0;
                }}
                .footer {{
                    font-size: 12px;
                    color: #888888;
                    margin-top: 30px;
                }}
            </style>
        </head>
        <body>
            <div class="container">
                <h2>Verification Code</h2>
                <p>Hello,</p>
                <p>Your verification code is:</p>
                <div class="code">{code}</div>
                <p>This code is valid for 10 minutes. Please do not share it with anyone.</p>
                <div class="footer">This is an automated email. Please do not reply.</div>
            </div>
        </body>
    </html>
    """
    from_email = settings.DEFAULT_FROM_EMAIL

    try:
        email_message = EmailMessage(
            subject=subject,
            body=html_content,
            from_email=from_email,
            to=[email]
        )
        email_message.content_subtype = 'html'  # 设置为 HTML
        email_message.send()
        logger.info(f"验证码邮件已成功发送给 {email}")
        return f"Email successfully sent to {email}"
    except Exception as e:
        logger.error(f"发送验证码邮件失败：{str(e)}")
        return f"Failed to send email: {e}"
