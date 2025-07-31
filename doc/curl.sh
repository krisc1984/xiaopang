curl -X POST \
        -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsInNpZ25fdHlwZSI6IlNJR04ifQ.eyJhcGlfa2V5IjoiYTdjZTZmNGFiMTI4ZDM0MDNhZTdiYjVlODllZGM2OTkiLCJleHAiOjE3MzI3NjU0MDIxOTIsInRpbWVzdGFtcCI6MTczMjc2MzYwMjE5Mn0.5Kdw7WPbAxTjYSoUYJR8O48QTC6tN3DzlfFvoSpnXkI" \
        -H "Content-Type: application/json" \
        -H "User-Agent: Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)" \
        -d '{
          "model":"glm-4",
          "stream": "true",
          "messages": [
              {
                  "role": "user",
                  "content": "1+1"
              }
          ]
        }' \
  https://open.bigmodel.cn/api/paas/v4/chat/completions
