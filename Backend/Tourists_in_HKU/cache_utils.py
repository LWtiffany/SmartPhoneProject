import redis
import random
import string

client = redis.Redis(host='localhost', port=6379, db=0)


def generate_code(length=6):
    """
    生成指定长度的随机验证码
    """
    return ''.join(random.choices(string.digits, k=length))


def store_verification_code(email, code, ttl=600):
    """
    存储验证码到 Redis
    """
    key = f"verify_code:{email}"
    client.setex(key, ttl, code)


def get_verification_code(email):
    """
    从 Redis 获取验证码
    """
    key = f"verify_code:{email}"
    return client.get(key)


def delete_verification_code(email):
    """
    删除验证码
    """
    key = f"verify_code:{email}"
    client.delete(key)
