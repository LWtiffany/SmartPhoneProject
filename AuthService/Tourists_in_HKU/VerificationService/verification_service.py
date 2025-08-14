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
        # 检查 Redis 中是否已存在验证码
        existing_code = get_verification_code(email)
        if existing_code:
            logger.info(f"邮箱 {email} 的验证码已存在，未重新生成")
            return {"status": 205, "message": "Verification code already sent"}

        # 生成验证码
        code = generate_code()

        # 存入 Redis，有效期10分钟
        store_verification_code(email, code)

        # 异步发送验证码邮件
        send_verification_email(email, code)  # 正确调用方式
        logger.info(f"验证码已发送至邮箱：{email}")
        return {"status": 200, "message": "Verification code sent"}
    except Exception as e:
        logger.error(f"验证码发送失败：{str(e)}")
        return {"status": 503, "message": "Failed to send verification code"}


def verify_code_service(email, code):
    """
    校验验证码
    """
    if request.method != 'POST':
        return JsonResponse({"status": "error", "message": "Method not allowed"}, status=405)

    email = request.POST.get("email")
    code = request.POST.get("code")

    if not email or not code:
        return JsonResponse({"status": "error", "message": "Email and code are required"}, status=400)

    # Step 1: 验证 Redis 中的验证码
    stored_code = r.get(f"verify_code:{email}")

    if stored_code is None:
        logger.warning(f"验证码过期或不存在：{email}")
        return JsonResponse({"status": "error", "message": "Verification code expired or not found"}, status=400)

    if stored_code != code:
        logger.warning(f"验证码不匹配：{email}")
        return JsonResponse({"status": "error", "message": "Verification failed"}, status=400)

    # Step 2: 查询 MySQL Booking 表
    has_booking = Booking.objects.filter(email=email).exists()
    logger.info(f"邮箱验证成功，是否预约：{email} → {has_booking}")

    return JsonResponse({
        "status": "success",
        "message": "Verification successful",
        "Booked": True
    }, status=200)
