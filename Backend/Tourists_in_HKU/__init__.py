from __future__ import absolute_import, unicode_literals
import logging

# 初始化日志记录器
logger = logging.getLogger(__name__)

# 导入任务函数，确保 Celery 自动发现
try:
    from .VerificationService.verification_service import send_verification_email
    logger.info("VerificationService: 成功加载邮件发送任务")
except ImportError as e:
    logger.error(f"VerificationService: 加载任务失败 - {str(e)}")

# 导入业务逻辑服务
try:
    from .VerificationService.verification_service import send_verification_code_service, verify_code_service
    logger.info("VerificationService: 成功加载验证码服务")
except ImportError as e:
    logger.error(f"VerificationService: 加载验证码服务失败 - {str(e)}")
