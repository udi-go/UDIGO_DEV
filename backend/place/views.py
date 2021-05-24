import json
from PIL import Image
from django.views import View
from django.http import JsonResponse, HttpResponse, QueryDict
from django.core.files.storage import FileSystemStorage

from server import settings
from .models import KakaoPlace, TourPlace, Review
from user.utils import login_decorator


def handle_uploaded_file(f):
    with open('some/file/name.jpeg', 'wb+') as destination:
        for chunk in f.chunks():
            destination.write(chunk)


# image upload & classification
class Classification(View):
    def get(self, img):
        pass

    def post(self, request):
        """
        파일명 수정 > time, idx
        """
        print(request.POST.get('image'))
        print(request.POST.get('title'))
        img = request.FILES['image']

        fs = FileSystemStorage()  # 이미지 파일을 저장할때 쓰는 함수
        filename = fs.save(img.name, img)
        uploaded_file_url = fs.url(filename)
        print(uploaded_file_url)

        return JsonResponse({'name': '공원'}, status=200)

    def _make_sentence(self, word):
        sentence_list = [
            ['멋진 ', '이네요!'],
            ['지금 놀러가기 딱 좋은 ', '이네요!'],
            ['오늘 ', '을 가고 싶으시군요!'],
            ['', ''],
            ['', ''],
        ]
        pass

"""
TO DO
1. 리뷰 수정 PATCH API > UserReviewView (PATCH)
2. 마이페이지 좋아요 GET API
"""


class PlaceReviewView(View):
    def get(self, request, place_id):
        place_type = request.GET.get('type')
        offset = int(request.GET.get('offset', 0))
        limit = int(request.GET.get('limit', 10))
        print(place_type, offset, limit, place_id)

        if place_type not in ['kakao', 'tour']:
            return JsonResponse({'message': 'INVALID_TYPE'}, status=400)

        if place_type == 'tour':
            reviews = Review.objects.select_related('user').filter(place_tour_id=place_id).order_by('-updated_at')[offset*limit : (offset+1)*limit]
        else:
            reviews = Review.objects.select_related('user').filter(place_kakao_id=place_id).order_by('-updated_at')[offset*limit : (offset+1)*limit]

        grade = 0
        count = 0
        result = []
        for review in reviews:
            result.append({
                'review_id': review.id,
                'user_id': review.user.id,
                'user_nickname': review.user.nickname,
                'grade': review.grade,
                'text': review.text,
                'date': review.updated_at
            })
            grade += review.grade
            count += 1

        grade = format(grade/count, ".2f")
        return JsonResponse({"reviews": result, 'grade': grade}, status=200)


class UserReviewView(View):
    @login_decorator
    def get(self, request):
        offset = int(request.GET.get('offset', 0))
        limit = int(request.GET.get('limit', 10))

        reviews = Review.objects.select_related('place_tour', 'place_kakao').filter(user_id=request.user).order_by('-updated_at')[offset*limit:(offset+1)*limit]
        reviews = [{
            'review_id': review.id,
            'type': review.place_type,
            'place_id': review.place_tour.id if review.place_type == 'tour' else review.place_kakao.id,
            'grade': review.grade,
            'text': review.text,
            'date': review.updated_at,
            'place_title': review.place_tour.title if review.place_type == 'tour' else review.place_kakao.title,
            'address': review.place_tour.address if review.place_type == 'tour' else review.place_kakao.address
        } for review in reviews]
        return JsonResponse({'reviews': reviews}, status=200)

    @login_decorator
    def post(self, request):
        try:
            place_type = request.POST.get('type')
            place_id = request.POST.get('place_id')

            if place_type not in ['kakao', 'tour']:
                return JsonResponse({'message': 'INVALID_TYPE'}, status=400)

            if place_type == 'tour':
                place = TourPlace.objects.filter(id=place_id)
                if not place.exists():
                    place = TourPlace(
                        id=place_id,
                        address=f"{request.POST.get('addr1')} {request.POST.get('addr2')}",
                        areacode=request.POST.get('areacode'),
                        cat1=request.POST.get('cat1'),
                        cat2=request.POST.get('cat2'),
                        cat3=request.POST.get('cat3'),
                        content_type_id=request.POST.get('content_type_id'),
                        createdtime=request.POST.get('createdtime'),
                        image1=request.POST.get('firstimage'),
                        image2=request.POST.get('firstimage2'),
                        mapx=request.POST.get('mapx'),
                        mapy=request.POST.get('mapy'),
                        modifiedtime=request.POST.get('modifiedtime'),
                        readcount=request.POST.get('readcount'),
                        sigungucode=request.POST.get('sigungucode'),
                        tel=request.POST.get('tel'),
                        title=request.POST.get('title')
                    ).save()
                else:
                    place = place[0]
                Review(
                    place_type=place_type,
                    place_tour=place,
                    user=request.user,
                    grade=request.POST.get('grade'),
                    text=request.POST.get('text')
                ).save()
            else:
                place = KakaoPlace.objects.filter(id=place_id)
                if not place.exists():
                    place = KakaoPlace(
                        id=place_id,
                        title=request.POST.get('place_name'),
                        place_url=request.POST.get('place_url'),
                        category_name=request.POST.get('category_name'),
                        category_group_code=request.POST.get('category_group_code'),
                        category_group_name=request.POST.get('category_group_name'),
                        tel=request.POST.get('phone'),
                        address=request.POST.get('address_name'),
                        road_address=request.POST.get('road_address_name'),
                        mapx=request.POST.get('x'),
                        mapy=request.POST.get('y')
                    ).save()
                else:
                    place = place[0]
                Review(
                    place_type=place_type,
                    place_kakao=place,
                    user=request.user,
                    grade=request.POST.get('grade'),
                    text=request.POST.get('text')
                ).save()
            return HttpResponse(status=201)
        except KeyError:
            return JsonResponse({'message': 'INVALID_KEY'}, status=400)

    @login_decorator
    def patch(self, request):
        try:
            query_dict = QueryDict(request.body)
            user = request.user
            review_id = query_dict['review_id']

            review = Review.objects.get(id=review_id, user=user)
            if 'text' in query_dict:
                review.text = query_dict['text']
            if 'grade' in query_dict:
                review.grade = query_dict['grade']

            review.save()
            return HttpResponse(status=200)
        except Review.DoesNotExist:
            return JsonResponse({'message': 'INVALID_REVIEW'}, status=400)
