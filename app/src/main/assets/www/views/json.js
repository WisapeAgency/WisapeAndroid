server {
    listen 80;
    server_name hw.techyoo.cn;
    access_log /alidata/log/nginx/hw.log;
    location / {
        root html;
    index 		   index.html index.php index.htm
    proxy_set_header   X-Real-IP            $remote_addr;
    proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
    proxy_set_header   Host                   $http_host;
    proxy_set_header   X-NginX-Proxy    true;
    proxy_set_header   Connection "";
    proxy_http_version 1.1;
}

location ~\.php$
{
    root /alidata/server/nginx/html;
    fastcgi_pass 127.0.0.1:9000;
    fastcgi_index index.php;
    fastcgi_param SCRIPT_FILENAME  $document_root/$fastcgi_script_name;
    include fastcgi_params;
}
}



server {
    listen 80;
    server_name st.sd188.cn;
    access_log /alidata/log/nginx/bs.log;
    location / {
        proxy_set_header   X-Real-IP            $remote_addr;
    proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
    proxy_set_header   Host                   $http_host;
    proxy_set_header   X-NginX-Proxy    true;
    proxy_set_header   Connection "";
    proxy_http_version 1.1;
    proxy_pass         http://st_upstream/;
        }
}