# -*- coding: utf-8 -*-
import os
import cv2
import csv
import json
import numpy as np
from PIL import Image
from pathlib import Path
from datetime import datetime
from django.views import View
from django.http import JsonResponse, HttpResponse, QueryDict
from django.core.files.storage import FileSystemStorage
from tensorflow.keras.models import load_model

from server import settings
from .models import KakaoPlace, TourPlace, Review, UserLikeTourPlace, UserLikeKakaoPlace
from user.utils import login_decorator
from tensorflow.keras.models import load_model


with open("place/place_23_index_to_label.json", "r", encoding="utf-8-sig") as f:
    label_info = json.load(f)
    # label_info = {v: k for k, v in label_info.items()}
    print(label_info)

model = load_model("place/model/place23_efnb0_3-0.26-0.92.h5")


# image upload & classification
class Classification(View):
    def inference(image_path):
        test = cv2.imread(image_path)
        test = cv2.cvtColor(test, cv2.COLOR_BGR2RGB)
        test = cv2.resize(test, (224, 224))
        test = test[np.newaxis, :, :, :]
        pred = model.predict(test, batch_size=1)
        return np.argmax(pred)

    def get(self, img):
        pass

    def post(self, request):
        """
        파일명 수정 > time, idx
        기능 추가 - 내가 검색했던 내용 볼 수 있게끔? > 예측결과도 저장????
        predict 참고 : https://github.com/Development-On-Saturday/AIFOODIE_PROJECT/blob/main/django_dev/foods/views.py
        """
        img = request.FILES['image']

        # 이미지 저장
        fs = FileSystemStorage()  # 이미지 파일을 저장할때 쓰는 함수
        now = datetime.now()
        img_name = f"{now.strftime('%Y%m%d%H%M%S')}.jpeg"
        filename = fs.save(img_name, img)
        uploaded_file_url = fs.path(filename)

        # 이미지 전처리 및 예측
        pred_index = self.inference(uploaded_file_url)
        pred = label_info[pred_index]
        print('예측 결과는 >>> ', pred_index, pred)

        # sentence 선택
        return JsonResponse({'name': pred, 'sentence': f'근사한 {pred}이네요!'}, status=200)


class PlaceReviewView(View):
    # 해당 플레이스에 대한 review, 별점 가져오기
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
                'date': review.updated_at.strftime("%Y-%m-%d %H:%M:%S")
            })
            grade += review.grade
            count += 1
        if count > 0:
            grade = format(grade/count, ".2f")
        else:
            grade = 0
        return JsonResponse({"reviews": result, 'grade': grade}, status=200)


class UserReviewView(View):
    @login_decorator
    def get(self, request):
        offset = int(request.GET.get('offset', 0))
        limit = int(request.GET.get('limit', 10))

        reviews = Review.objects.select_related('place_tour', 'place_kakao').filter(user_id=request.user).order_by('-updated_at')[offset*limit:(offset+1)*limit]

        result = [{
            'review_id': review.id,
            'type': review.place_type,
            'place_id': review.place_tour.id if review.place_type == 'tour' else review.place_kakao.id,
            'grade': review.grade,
            'text': review.text,
            'date': review.updated_at.strftime("%Y-%m-%d %H:%M:%S"),
            'place_title': review.place_tour.title if review.place_type == 'tour' else review.place_kakao.title,
            'address': review.place_tour.address if review.place_type == 'tour' else review.place_kakao.address,
            'image': review.place_tour.image1 if review.place_type == 'tour' and review.place_tour.image1 is not None else ""
        } for review in reviews]
        return JsonResponse({'reviews': result}, status=200)

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
                        sigungucode=request.POST.get('sigungucode'),
                        tel=request.POST.get('tel'),
                        title=request.POST.get('title'),
                        overview=request.POST.get('overview'),
                        zipcode=request.POST.get('zipcode'),
                        homepage=request.POST.get('homepage')
                    ).save()
                place = TourPlace.objects.filter(id=place_id)
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
                    KakaoPlace(
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
                place = KakaoPlace.objects.filter(id=place_id)
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


class PlaceLikeView(View):
    @login_decorator
    def post(self, request):
        place_type = request.POST.get('type')
        place_id = request.POST.get('place_id')
        user = request.user

        if not place_type:
            return JsonResponse({'message': 'PLACE_TYPE_ERROR'}, status=400)

        try:
            if place_type not in ['kakao', 'tour']:
                return JsonResponse({'message': 'INVALID_PLACE_TYPE'}, status=400)

            if place_type == 'tour':
                print('tour check')
                like = UserLikeTourPlace.objects.get(place=place_id, user=user)
            else:
                print('kakao check')
                like = UserLikeKakaoPlace.objects.get(place=place_id, user=user)

            like.delete()
            result = False

        except UserLikeTourPlace.DoesNotExist:
            if not TourPlace.objects.filter(id=place_id).exists():
                TourPlace(
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
                    sigungucode=request.POST.get('sigungucode'),
                    tel=request.POST.get('tel'),
                    title=request.POST.get('title'),
                    overview=request.POST.get('overview'),
                    zipcode=request.POST.get('zipcode'),
                    homepage=request.POST.get('homepage')
                ).save()
            UserLikeTourPlace(
                place_id=place_id,
                user=user
            ).save()
            result = True
        except UserLikeKakaoPlace.DoesNotExist:
            if not KakaoPlace.objects.filter(id=place_id).exists():
                KakaoPlace(
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
            UserLikeKakaoPlace(
                place_id=place_id,
                user=user
            ).save()
            result = True
        return JsonResponse({'message': result}, status=200)


class UserLikeView(View):
    # 유저가 누른 좋아요 장소에 대한 모든 리스트 가져오기
    @login_decorator
    def get(self, request):
        user = request.user
        places = UserLikeTourPlace.objects.select_related('place').filter(user=user).order_by('-created_at')
        response = {
            'all': [],
            'a12': [],
            'a14': [],
            'a15': [],
            'a28': [],
            'a32': [],
            'a38': [],
            'a39': []
        }
        for place in places:
            data = {
                'place_id': place.place.id,
                'type': 'tour',
                'content_type_id': place.place.content_type_id,
                'title': place.place.title,
                'image': place.place.image1,
                'address': place.place.address,
                'created_at': place.created_at.strftime("%Y-%m-%d %H:%M:%S")
            }
            response['all'].append(data)
            response['a'+str(place.place.content_type_id)].append(data)
        return JsonResponse(response, status=200)