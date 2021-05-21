# Generated by Django 3.2.3 on 2021-05-21 15:39

from django.db import migrations, models


class Migration(migrations.Migration):

    initial = True

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='User',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('social_type', models.IntegerField(choices=[(0, 'kakao'), (1, 'naver'), (2, 'google')], default=0)),
                ('kakao_id', models.IntegerField(null=True)),
                ('naver_id', models.IntegerField(null=True)),
                ('google_id', models.IntegerField(null=True)),
                ('nickname', models.CharField(max_length=25)),
                ('created_at', models.DateTimeField(auto_now_add=True)),
            ],
            options={
                'db_table': 'users',
            },
        ),
    ]
