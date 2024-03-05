from lxml import html

etree = html.etree
import requests
import sys


# 根据关键词获取项目列表
def get_repos_list(key_words):
    url = 'https://github.com/search?q=' + key_words + '&type=repositories'
    requests.packages.urllib3.disable_warnings()
    response = requests.get(url, verify=False)
    # 获取页面源码
    page_source = response.text
    # print(page_source)
    tree = etree.HTML(page_source)
    # 获取项目超链接
    arr = tree.xpath('//*[@class="f4 text-normal"]/a/@href')
    repos_name = arr[0]
    return repos_name


# 获取一个项目的issues列表
def get_issues_list(repo_name, author):
    issues_list = []
    time_list = []
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
    # print(number)
    page = int(number)

    for i in range(1, page):
        url = 'https://github.com' + repo_name + '/issues?page=' + str(i)
        #print(url)
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
        issues_name = tree.xpath('//*[@class="d-flex mt-1 text-small color-fg-muted"]/span/a/@title')
        # print("name0", issues_name[0][23:])
        for j in range(0, len(issues_datetime) - 1):
            # 打印issue url
            url = 'https://github.com' + arr[j]
            # 判断是否为指定人
            issue_datetime = issues_datetime[j][:10]
            issue_author = issues_name[j][23:]
            if issue_author == author:

                issues_list.append(arr[j])
                time_list.append(issue_datetime)

        # /combust/mleap/issues/716
    # 返回issues数量和列表

    return number, issues_list, time_list


# 获取一个issue的内容及评论
def get_issue_content(issue_name):
    # 拼接issue地址
    url = 'https://github.com' + issue_name
    #print(url)
    requests.packages.urllib3.disable_warnings()
    response = requests.get(url, verify=False)
    page_source = response.text
    tree = etree.HTML(page_source)
    issue_content = tree.xpath('//table//td')[0].xpath('string(.)')
    content = ' '.join(str(issue_content).replace('\n', '.').split())
    return content


if __name__ == '__main__':

    key_words = sys.argv[1]
    author = sys.argv[2]
    file_name = str(key_words.split('/')[1]) + ' ' + author + '.txt'

    # 获取项目列表
    repos_name = get_repos_list(key_words)
    # 拼接项目url
    repos_url = 'https://github.com' + repos_name
    # 获取项目的issues列表
    number, issues_list, time_list = get_issues_list(repos_name, author)

    for k in range(0, len(issues_list)):
        issue = issues_list[k]
        # 获取issue的内容
        issue_url = 'https://github.com' + issue
        content = get_issue_content(issue)
        # content=filter_emoji(content)
        print(str(time_list[k]) + str(content).strip())


