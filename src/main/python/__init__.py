# -*- coding: utf-8 -*-
import urllib
import urllib2
import random
# import urlparse
# import cookielib
import sys
import re
import time
import os
from compress import ContentEncodingProcessor

# reload(sys)
# sys.setdefaultencoding('utf8')
if sys.getdefaultencoding() != 'utf-8':
    reload(sys)
    sys.setdefaultencoding('utf-8')


def title_check(title):  # 检查标题
    limit = ['<', '>', '/', '\\', '|', '\"', '*', '?', ':']
    for i in limit:
        title = title.replace(i, '')
    if len(title) > 200:
        title = title[0:200]
    return title


'''
User-Agent信息，防止403
'''
headers = [
    "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36",
    "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:30.0) Gecko/20100101 Firefox/30.0"
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/537.75.14",
    "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; Win64; x64; Trident/6.0)"
]
proxy = '127.0.0.1:1080'  # ss代理位置,localhost,端口1080

'''
登录，保存cookie到文件
def login(url, proxy, logdata):
    filename = 'cookie.txt'
    cookie = cookielib.MozillaCookieJar(filename)  # cookie
    randdom_header = random.choice(headers)  # header
    proxy_params = {urlparse.urlparse(url).scheme: proxy}  # proxy
    req = urllib2.Request(url)
    req.add_header("User-Agent", randdom_header)
    opener = urllib2.build_opener(urllib2.HTTPCookieProcessor(cookie))  # 添加cookie
    opener.add_handler(urllib2.ProxyHandler(proxy_params))  # 添加代理

    content = opener.open(req, logdata).read()
    cookie.save(ignore_discard=True, ignore_expires=True)  # 保存cookie
    return content  # 根据返回内容，判断是否登陆成功
'''

def get_host_by_url(url):
    proto, rest = urllib.splittype(url)
    host, rest = urllib.splithost(rest)
    return host

def get_exh_req(url, is_image):
    # proxy_params = {urlparse.urlparse(url).scheme: proxy}  # proxy
    # filename = 'cookie.txt'
    # cookie = cookielib.MozillaCookieJar(filename)  # cookie
    randdom_header = random.choice(headers)
    req = urllib2.Request(url)
    # req.add_header("User-Agent", randdom_header)
    if is_image:
        req.add_header('Accept', 'image/png, image/svg+xml, image/jxr, image/*; q=0.8, */*; q=0.5')
    else:
    # cookie.load(ignore_discard=True, ignore_expires=True)
        req.add_header("Accept", 'text/html, application/xhtml+xml, image/jxr, */*')
        req.add_header('Cookie',
                   'ipb_member_id=2798962; ipb_pass_hash=24576fff1f871b0feff9c4fbfeba4d92; yay=louder; \
                   igneous=a5854c5a1; lv=1527562527-1527562527; s=e3a51d202; sk=qkpbkq4ifredzvkfr9a5ubuizu67')
    # req.add_header("Accept-Encoding", 'gzip, deflate')
    req.add_header('Accept-Language', 'zh-Hans-CN,zh-Hans,q=0.5')
    req.add_header('Connection', 'Keep-Alive')
    host = get_host_by_url(url)
    req.add_header('Host', host)

    return req
    # urllib2.HTTPCookieProcessor(cookie)
    # opener.add_handler(urllib2.ProxyHandler(proxy_params))  # 添加代理


'''
获取网页内容
'''


def get_html(url, is_image):
    encoding_support = ContentEncodingProcessor
    opener = urllib2.build_opener(encoding_support, urllib2.HTTPHandler)
    req = get_exh_req(url, is_image)
    content = ''
    refresh_time = 3  # 超时刷新次数
    while content == '' and refresh_time > 0:
        try:
            req.add_header('User-Agent',
                           random.choice(headers))
            res = opener.open(req, timeout=30)
            if res.code =='403':
                print '403，服务器拒绝访问，直接退出程序！'
                sys.stdout.flush()
                os._exit(1)
            content = res.read()
        except Exception, e:
            print e
            content = ''
            print '网络连接超时，开始尝试第' + str(4 - refresh_time) + '次重连'
            sys.stdout.flush()
        refresh_time -= 1
    opener.close()
    interval = random.uniform(2, 3)
    time.sleep(interval)
    return content


'''
获取标题
'''


def get_title(html):
    title_pattern = '<title>([\d\D]*)</title>'
    title = re.search(title_pattern, html).group(1)
    return title


'''
总长度
'''


def get_length(html):
    length_pattern = '(\d*) pages'
    pages = re.search(length_pattern, html).group(1)
    return int(pages)


def get_real_length(html):
    length_pattern = '<div><span>([\d]*)</span> / <span>([\d]*)</span></div>'
    obj = re.search(length_pattern, html)
    current_page = int(obj.group(1))
    total_page = int(obj.group(2))
    return [current_page, total_page]


'''
找到第一个图片的页面URL
'''


def get_first_page_url(html):
    url_pattern = 'a href="(https://exhentai.org/s/[\d\D]*?-1)'
    url = re.search(url_pattern, html).group(1)
    return url


'''
找到网页中的图片地址
'''


def find_image_url(html):
    image_pattern = 'img id="img" src="([\d\D]*?)"'
    url = re.search(image_pattern, html).group(1)
    return url


'''
根据图片URL，下载图片
'''


def download_image(image_url, file_name):
    binary_data = get_html(image_url, True)
    if not binary_data:
        return False
    temp_file = open(file_name, 'wb')
    temp_file.write(binary_data)
    temp_file.close()
    return True


'''
找到后续的地址
'''


def find_next_url(html):
    next_pattern = '<a id="next"[\d\D]*?href="([\d\D]*?)">'
    obj = re.search(next_pattern, html)
    if obj:
        return obj.group(1)
    else:
        return None


'''
    获得文件后缀名
'''


def get_suffix(url):
    suffix_pattern = '[\d\D]+\.([\d\D]*)'
    return re.search(suffix_pattern, url).group(1)


'''
采用先保存所有的图片URL，然后统一下载的策略
'''


def exh_download_with_current_page(current_page_url, num):
    exception_message = '网络连接发生异常...退出！'
    parent_path = 'F:\\images\\'
    html = get_html(current_page_url, False)
    if html == '':
        print exception_message
        sys.stdout.flush()
        return None
    title = get_title(html)
    print '开始下载 ' + title + ' 保存路径F:\\images\\标题名'
    sys.stdout.flush()
    dir_name = title_check(title).encode('gbk')
    path = parent_path + dir_name
    if not os.path.isdir(path):
        os.makedirs(path)
    page_message_list = get_real_length(html)
    current_page = page_message_list[0]
    total_page = page_message_list[1]
    length = total_page - current_page + 1
    if length < num or num <= 0:
        num = length
    image_url_list = []
    page_url_list = []
    current_url = current_page_url
    print '开始遍历要下载的图片所在网页...'
    sys.stdout.flush()
    num += 1
    for i in range(1, num):
        current_html = get_html(current_url, False)
        if not current_html:
            print exception_message
            sys.stdout.flush()
            return None
        image_url = find_image_url(current_html)
        next_url = find_next_url(current_html)
        page_url_list.append(current_url)
        image_url_list.append(image_url)
        print '正在遍历第%s个,收集信息中...' % i
        sys.stdout.flush()
        if next_url:
            current_url = next_url
        else:
            break
    fail_dict = {'path': path}
    for i in range(1, len(image_url_list) + 1):
        file_name = i + current_page - 1

        url_item = image_url_list[i - 1]
        suffix = get_suffix(url_item)
        current_path = path + '\\%s.' % file_name + suffix
        if download_image(url_item, current_path):
            print '系列第%s张下载成功！' % file_name
        else:
            print '系列第%s张下载失败！' % file_name
            fail_dict[str(file_name)] = url_item
        sys.stdout.flush()
    print '下载结束！'
    sys.stdout.flush()
    return fail_dict


def exh_download(url, num):
    exception_message = '网络连接发生异常...退出！'
    html = get_html(url, False)
    if html == '':
        print exception_message
        sys.stdout.flush()
        return None
    first_page_url = get_first_page_url(html)
    return exh_download_with_current_page(first_page_url, num)


'''
尝试下载失败字典中的图
'''


def download_fail_dict(fail_dict):
    less_fail_dict = {}
    path = fail_dict['path']
    less_fail_dict['path'] = path
    del fail_dict['path']
    for key in fail_dict:

        value = fail_dict[key]
        suffix = get_suffix(value)
        current_path = path + '\\%s.' % key + suffix
        if download_image(value, current_path):
            print '第%s张下载成功！' % key
        else:
            print '第%s张下载失败！' % key
            less_fail_dict[key] = value
        sys.stdout.flush()
    return less_fail_dict


'''
接收输入
'''


def main():
    #url_str = raw_input('输入开始的地址:')
    #number = int(raw_input('输入数量:'))
    input_str = raw_input()
    url_str = input_str.split(',')[0]
    number = int(input_str.split(',')[1])

    if '-' in url_str:
        fail_dict_instance = exh_download_with_current_page(url_str, number)
    else:
        fail_dict_instance = exh_download(url_str, number)
    redownload_number = 3
    while fail_dict_instance and redownload_number > 0:
        fail_dict_instance = download_fail_dict(fail_dict_instance)
        redownload_number -= 1


if __name__ == '__main__':
    main()
# print get_html('https://exhentai.org/')
'''
edge浏览器请求头，Accept-Encoding不要
Accept:text/html, application/xhtml+xml, image/jxr, */*
Accept-Encoding:gzip, deflate
Accept-Language:zh-Hans-CN,zh-Hans,q=0.5
Connection:Keep-Alive
Cookie: ipb_member_id=2798962; ipb_pass_hash=24576fff1f871b0feff9c4fbfeba4d92; yay=louder; igneous=a5854c5a1; lv=1527562527-1527562527; s=e3a51d202; sk=qkpbkq4ifredzvkfr9a5ubuizu67
Host: exhentai.org
User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393
'''
'''
图片的下载头
Accept： image/png, image/svg+xml, image/jxr, image/*; q=0.8, */*; q=0.5
Cookie 没有
host：对应的
'''