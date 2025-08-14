# 项目目录结构

## Backend/ # 项目根目录  
├── Tourists_in_HKU/ # 应用目录  
│   ├── migrations/ # 存放数据库迁移文件  
│   ├── templates/ # HTML模板文件  
│   │   └── Tourists_in_HKU/  
│   │       ├── book_tour.html # 预约表单页面  
│   │       └── success.html # 预约成功页面  
│   ├── __init__.py # 标识为Python包  
│   ├── admin.py # Django管理后台配置  
│   ├── apps.py # 应用配置类  
│   ├── forms.py # 表单定义（预约表单）  
│   ├── models.py # 数据模型（Booking和Video）  
│   ├── tests.py # 单元测试文件    
│   ├── SendEmail.py #预约成功发送邮件功能   
│   ├── urls.py # 应用级URL路由配置  
│   └── views.py # 视图函数（业务逻辑）  

├── Backend/ # 项目配置目录  
│   ├── __init__.py # 标识为Python包  
│   ├── settings.py # 项目全局配置（数据库、应用等）  
│   ├── urls.py # 项目级URL路由配置  
│   ├── asgi.py # ASGI服务器配置  
│   └── wsgi.py # WSGI服务器配置  

├── manage.py # Django命令行工具入口  
└── requirements.txt # 项目依赖列表  

## Description of Database
Table <auth~> deny illegal login in system

Table <Django~> Django system intial files

## How to Access

本地访问：http://localhost:8000/tourists/book/

局域网访问：http://<你的IP>:8000/tourists/book/


