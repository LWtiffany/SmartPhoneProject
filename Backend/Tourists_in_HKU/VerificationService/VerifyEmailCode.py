import redis
import logging

logger = logging.getLogger(__name__)

# 连接 Redis
r = redis.StrictRedis(host='localhost', port=6379, db=0, decode_responses=True)


def get_verification_code(email):
    """从 Redis 获取验证码"""
    key = f"verify_code:{email}"
    code = r.get(key)
    if code:
        logger.info(f"从 Redis 中获取验证码：{code} 对应邮箱：{email}")
    else:
        logger.info(f"Redis 中未找到验证码，邮箱：{email}")
    return code


def verify_verification_code_service(email, code):
    """验证验证码是否正确"""
    stored_code = get_verification_code(email)
    if stored_code is None:
        logger.warning(f"验证码不存在或已过期：{email}")
        return {"status": "error", "message": "Verification code expired or not found"}
    if stored_code == code:
        logger.info(f"验证码验证成功：{email}")
        return {"status": "success", "message": "Verification successful"}
    else:
        logger.warning(f"验证码验证失败：{email}")
        return {"status": "error", "message": "Verification failed"}
