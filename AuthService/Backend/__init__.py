# 设置 pymysql 作为 MySQLdb
import pymysql
pymysql.install_as_MySQLdb()

# 加载 Celery 应用实例
from .celery import app as celery_app

# 将 Celery 应用实例放入全局命名空间
__all__ = ('celery_app',)