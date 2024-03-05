import sys

from lxml import html
etree = html.etree
import requests
import os

# 根据关键词获取项目列表
def get_repos_list(key_words):
    # 初始化列表
    # 默认

    url = 'https://github.com/search?q=' + key_words + '&type=repositories'
    requests.packages.urllib3.disable_warnings()
    response = requests.get(url, verify=False)
    # 获取页面源码
    page_source = response.text

    tree = etree.HTML(page_source)
    # 获取项目超链接
    arr = tree.xpath('//*[@class="f4 text-normal"]/a/@href')

    repos_name = arr[0]

    return repos_name


# 获取一个项目的issues列表
def get_issues_list(repo_name,after_datetime,before_datetime):
    issues_list = []
    url = 'https://github.com' + repo_name + '/issues'
    requests.packages.urllib3.disable_warnings()
    response = requests.get(url, verify=False)
    # 获取源码
    page_source = response.text
    tree = etree.HTML(page_source)
    # 获取issues页面数量
    number = tree.xpath('//*[@class="paginate-container d-none d-sm-flex flex-sm-justify-center"]/div/a[6]')
    if len(number) == 0:
        number = '0'
    else:
        number = number[0].text
    page = int(number)

    for i in range(1, page):
        url = 'https://github.com' + repo_name + '/issues?page=' + str(i)
        requests.packages.urllib3.disable_warnings()
        response = requests.get(url, verify=False)
        # 获取源码
        page_source = response.text
        tree = etree.HTML(page_source)
        # 获取issues超链接
        arr = tree.xpath(
            '//*[@class="flex-auto min-width-0 p-2 pr-3 pr-md-2"]/a/@href')
        # issue的更新时间
        issues_datetime = tree.xpath(
            '//*[@class="d-flex mt-1 text-small color-fg-muted"]/span/relative-time/@datetime')

        for j in range(0, len(issues_datetime) - 1):
            # 打印issue url
            url = 'https://github.com' + arr[j]

            # 判断issue更新时间是否满足条件
            issue_datetime = issues_datetime[j][:10]

            # 打印issue更新时间

            if issue_datetime > after_datetime and issue_datetime < before_datetime:

                issues_list.append(arr[j])

        # /combust/mleap/issues/716
    # 返回issues数量和列表

    return number, issues_list


# 获取一个issue的内容及评论
def get_issue_content(issue_name):
    # 拼接issue地址
    url = 'https://github.com' + issue_name
    requests.packages.urllib3.disable_warnings()
    response = requests.get(url, verify=False)
    page_source = response.text
    tree = etree.HTML(page_source)
    # 获取issue内容
    # issue_content = tree.xpath('//table//td//p[1]//text()')

    issue_content = tree.xpath('//table//td')[0].xpath('string(.)')
    content = ' '.join(str(issue_content).replace('\n','.').split())
    # print(issue_content)
    return content


if __name__ == '__main__':

    key_words = sys.argv[1]
    version = sys.argv[2]
    start_time = sys.argv[3]
    end_time = sys.argv[4]

    # 获取项目列表
    repos_name = get_repos_list(key_words)

    # 拼接项目url
    repos_url = 'https://github.com' + repos_name


    # 获取项目的issues列表
    number, issues_list = get_issues_list(repos_name, start_time, end_time)

    for issue in issues_list:
        # 获取issue的内容
        issue_url = 'https://github.com' + issue
        content = get_issue_content(issue)
        print(content)





