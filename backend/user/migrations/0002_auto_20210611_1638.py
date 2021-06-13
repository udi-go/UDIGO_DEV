# Generated by Django 3.2.3 on 2021-06-11 16:38

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('user', '0001_initial'),
    ]

    operations = [
        migrations.RemoveField(
            model_name='user',
            name='google_id',
        ),
        migrations.RemoveField(
            model_name='user',
            name='kakao_id',
        ),
        migrations.RemoveField(
            model_name='user',
            name='naver_id',
        ),
        migrations.AddField(
            model_name='user',
            name='social_id',
            field=models.IntegerField(default=0),
            preserve_default=False,
        ),
    ]