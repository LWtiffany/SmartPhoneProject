from django.http import HttpResponse
from django.shortcuts import render
from .forms import BookingForm
from .SendEmail import send_success_email  # 导入封装好的发送邮件方法


def book_tour(request):
    if request.method == 'POST':
        form = BookingForm(request.POST)
        # TODO judge the input valid
        if form.is_valid():
            # 保存表单数据到数据库
            form.save()
            # TODO redesign the database
            email = form.cleaned_data['email']
            username = form.cleaned_data['name']
            # Send Email
            send_success_email(email, username)

            return HttpResponse('Booking completed!', status=200)
        else:
            return HttpResponse('Not success', status=503)
    elif request.method == 'GET':
        # GET 方法仅在开发环境测试
        form = BookingForm()
        return render(request, 'Tourists_in_HKU/book_tour.html', {'form': form})
    else:
        return HttpResponse("Wrong method", status=405)  # 返回405方法不允许错误

# def success(request):
#     return render(request, 'Tourists_in_HKU/success.html')


# def get_video(request):
#     latitude = request.GET.get('latitude')
#     longitude = request.GET.get('longitude')
#     video = Video.objects.filter(latitude=latitude, longitude=longitude).first()
#     if video:
#         return JsonResponse({'video_url': video.video_file.url})
#     else:
#         return JsonResponse({'error': 'Video not found'}, status=404)
