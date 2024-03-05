import sys

from lxml import html
etree = html.etree
import requests



# 获取一个项目的issues列表
def get_repo_persons(repo_name):
    url = 'https://github.com' + repo_name + '/issues/show_menu_content?partial=issues%2Ffilters%2Fauthors_content&q=is%3Aissue+is%3Aopen'
    response = requests.get(url, verify=False)
    # 获取源码
    page_source = response.text
    tree = etree.HTML(page_source)
    # 获取issues页面人名下拉列表
    persons = tree.xpath('//strong[@class="mr-2"]/text()')
    # print(persons)
    return persons


if __name__ == '__main__':

    key_words = sys.argv[1]
    # 获取项目列表
    repos_name = '/' + key_words
    n = get_repo_persons(repos_name)
    print(n)