# Django 旅游预约系统 API 文档

## 概述
本系统提供两个核心 API：
1. **预约功能**：接收用户预约信息并写入数据库。
2. **视频返回功能**：根据坐标信息返回视频。

---

## 1. 预约功能

### 接口地址
POST /tourists/book/
### 请求参数
| 参数名 | 类型   | 是否必填 | 描述         |
|--------|--------|----------|--------------|
| name   | string | 是       | 用户姓名     |
| email  | string | 是       | 用户邮箱     |
| phone  | string | 是       | 用户电话     |
| date   | string | 是       | 预约日期     |
| time   | string | 是       | 预约时间     |
| notes  | string | 否       | 备注信息     |

### 请求示例
```json
{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "1234567890",
    "date": "2023-10-25",
    "time": "14:00",
    "notes": "需要导游"
}


{
    "status": "200",
    "message": "Appointment successful! We have sent a confirmation email to your email."
}
{
    "status": "405",
   
}