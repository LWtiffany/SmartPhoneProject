# HKUtopia
---
# Backend Description

## 项目目录结构 / Project Directory Structure

```
Backend/                           # 项目根目录 / Project root
├── manage.py                      # Django 命令行工具入口 / Django management entry
├── requirements.txt               # 依赖列表 / Python dependencies
├── Backend/                       # 项目配置目录 / Project configuration
│   ├── __init__.py
│   ├── settings.py                # 全局配置 / Global settings
│   ├── urls.py                    # 全局路由 / Main URL routes
│   ├── asgi.py                    # ASGI 配置 / ASGI configuration
│   └── wsgi.py                    # WSGI 配置 / WSGI configuration
├── Tourists_in_HKU/               # 应用目录 / Main app
│   ├── migrations/                # 数据库迁移文件 / DB migrations
│   ├── templates/
│   │   └── Tourists_in_HKU/       # HTML 模板 / HTML templates
│   │       ├── book_tour.html     # 预约表单页面 / Booking form
│   │       ├── success.html       # 预约成功页面 / Success page
│   │       ├── sendemail.html     # 邮件内容模板 / Email template
│   │       └── verify.html        # 验证页面 / Email verification
│   ├── __init__.py
│   ├── admin.py                   # Django 管理后台 / Django admin settings
│   ├── apps.py                    # 应用配置 / App config
│   ├── forms.py                   # 表单定义 / Form definitions
│   ├── models.py                  # 数据模型 / Data models
│   ├── tests.py                   # 单元测试 / Unit tests
│   ├── SendEmail.py               # 发送邮件模块 / Send email module
│   ├── urls.py                    # 应用级路由 / App-level routes
│   └── views.py                   # 视图函数 / Business logic
│
├── VerificationService/           # 验证服务 / Verification service
│   ├── VerifyEmailCode.py         # 邮箱验证码处理 / Email code logic
│   ├── verification_service.py    # 验证服务 / Verification service
│   └── verify.py                  # 其它验证逻辑 / Other verify logic
│
├── logs/                          # 日志目录 / Log files
│   ├── Tourists_in_hku.log
│   └── Description.md
```

---

## 数据库说明 / Database Description

- 表 `<auth~>`：系统用户认证与非法登录拦截  
  `<auth~>`: For system user authentication and denying illegal login.
- 表 `<Django~>`：Django 系统初始化文件  
  `<Django~>`: Initial files for Django system.

---

## 如何运行 / How to Run

1. **安装依赖 / Install dependencies**  
   ```
   pip install -r requirements.txt
   ```

2. **数据库迁移 / Run migrations**  
   ```
   python manage.py migrate
   ```

3. **启动开发服务器 / Start development server**  
   ```
   python manage.py runserver
   ```

4. **访问系统 / Access the system**  
   - 本地访问 / Local:  
     [http://localhost:8000/tourists/book/](http://localhost:8000/tourists/book/)
   - 局域网访问 / Intranet:  
     `http://<你的IP>:8000/tourists/book/`

---

## 其它说明 / Other Notes

- **管理员后台 / Admin panel**  
  访问 [http://localhost:8000/admin/](http://localhost:8000/admin/)  
  (需要先创建超级用户 `python manage.py createsuperuser`)

- **邮件配置 / Email settings**  
  请在 `Backend/settings.py` 中配置邮件服务器（用于发送预约确认邮件）。  
  Configure your email backend in `Backend/settings.py` for sending confirmation emails.

---

## 联系与支持 / Contact & Support

如有问题请联系项目开发者。  
For questions, please contact the project developer.

---

> 本 README 支持中英文，欢迎补充完善！  
> This README supports both Chinese and English. Feel free to improve or update it!

---

如需调整细节，欢迎继续补充你的个性化说明！
