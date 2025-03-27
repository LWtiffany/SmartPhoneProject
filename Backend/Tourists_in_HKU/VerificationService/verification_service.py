import logging
from Tourists_in_HKU.cache_utils import generate_code, store_verification_code, get_verification_code
from Tourists_in_HKU.VerificationService.verify import send_verification_email
# from django.core.validators import validate_email
# from django.core.exceptions import ValidationError
# from django.http import JsonResponse, HttpResponseNotAllowed

logger = logging.getLogger(__name__)





def send_verification_code_service(email):
    """
    生成验证码并发送邮件
    """
    try:
        # 生成验证码
        code = generate_code()
        # 存入 Redis，有效期10分钟
        store_verification_code(email, code)
        # 异步发送验证码邮件
        send_verification_email.delay(email, code)  # 正确调用方式
        logger.info(f"验证码已发送至邮箱：{email}")
        return {"status": "success", "message": "Verification code sent"}
    except Exception as e:
        logger.error(f"验证码发送失败：{str(e)}")
        return {"status": "error", "message": "Failed to send verification code"}



def verify_code_service(email, code):
    """
    校验验证码
    """
    try:
        stored_code = get_verification_code(email)
        if stored_code and stored_code.decode() == code:
            # 校验成功后删除验证码
            logger.info(f"验证码校验成功：{email}")
            return {"status": 200, "message": "Code verified"}
        else:
            logger.warning(f"验证码校验失败：{email}")
            return {"status": 503, "message": "Invalid or expired code"}
    except Exception as e:
        logger.error(f"验证码校验异常：{str(e)}")
        return {"status": "error", "message": "Verification failed"}
