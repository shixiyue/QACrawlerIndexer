'''
Sample post:
<row Id="1" PostTypeId="1" AcceptedAnswerId="8" CreationDate="2012-12-11T20:37:08.823" Score="70" ViewCount="25454" Body="&lt;p&gt;Assuming the world in the One Piece universe is round, then there is not really a beginning or an end of the Grand Line.&lt;/p&gt;&#xA;&#xA;&lt;p&gt;The Straw Hats started out from the first half and are now sailing across the second half.&lt;/p&gt;&#xA;&#xA;&lt;p&gt;Wouldn't it have been quicker to set sail in the opposite direction from where they started?     &lt;/p&gt;&#xA;" OwnerUserId="21" LastEditorUserId="1398" LastEditDate="2015-04-17T19:06:38.957" LastActivityDate="2015-05-26T12:50:40.920" Title="The treasure in One Piece is at the end of the Grand Line. But isn't that the same as the beginning?" Tags="&lt;one-piece&gt;" AnswerCount="5" CommentCount="0" FavoriteCount="3" />
'''

data_path = 'data/'
data_file_name = '/Posts.xml'
parsed_data_path = 'parsed/'

LINK_PREFIX = 'http://'
LINK_SUFFIX = '/questions/'
QUESTION_TYPE_ID = '1'
ANSWER_TYPE_ID = '2'

ID = 'Id'
BODY = 'Body'
POST_TYPE_ID = 'PostTypeId'
SCORE = 'Score'
TITLE = 'Title'
TAGS = 'Tags'
PARENT_ID = 'ParentId'

URL = 'url'
CATEGORIES = 'categories'
QUESTION = 'question'
DESCRIPTION = 'description'
VOTE = 'vote'
ANSWERS = 'answers'
ANSWER = 'answer'

special_websites = {
    '3dprinting': ['3D printing'],
    'askubuntu': ['ubuntu'],
    'avp': ['video production'],
    'beer': ['alcohol'],
    'boardgames': ['board game'],
    'codegolf': ['code golf', 'programming puzzle'],
    'codereview': ['code review'],
    'cogsci': ['cognitive science'],
    'computergraphics': ['computer graphics'],
    'craftcms': ['Craft CMS'],
    'cs': ['computer science', 'cs'],
    'cstheory': ['computer science theory'],
    'datascience': ['data science'],
    'dsp': ['signal processing', 'dsp'],
    'dba': ['database', 'dba'],
    'earthscience': ['earth science'],
    'elementaryos': ['elementary os'],
    'ell': ['english'],
    'gamedev': ['game development'],
    'graphicdesign': ['graphic design'],
    'ham': ['radio'],
    'hardwarerecs': ['hardware'],
    'hermeneutics': ['Biblical Hermeneutics'],
    'hsm': ['history of science and mathematics'],
    'iot': ['iot', 'internet of things'],
    'languagelearning': ['language learning'],
    'martialarts': ['martial arts'],
    'matheducators': ['mathematics educators', 'math'],
    'mathoverflow': ['math', 'mathematics'],
    'moderators': ['community'],
    'money': ['personal finance', 'money'],
    'musicfans': ['music fans'],
    'networkengineering': ['network engineering'],
    'opendata': ['open data'],
    'opensource': ['open source'],
    'pm': ['project management', 'pm'],
    'quant': ['quantitative finance', 'quant'],
    'raspberrypi': ['raspberry pi'],
    'reverseengineering': ['reverse engineering'],
    'scicomp': ['computational science'],
    'serverfault': ['system administration'],
    'softwarerecs': ['software'],
    'sqa': ['sqa', 'software quality assurance'],
    'stackapps': ['stack exchange api'],
    'stackoverflow': ['programming'],
    'stats': ['stats', 'statistics'],
    'superuser': ['computer'],
    'tex': ['tex', 'latex'],
    'ux': ['user experience', 'ux'],
    'vi': ['vi', 'vim'],
    'webapps': ['web application', 'app'],
    'windowsphone': ['windows phone'],
    'woodworking': ['wood']
}

def default_categories(website):
    split_website = website.split('.')
    if split_website[0] in special_websites:
        return list(special_websites[split_website[0]])
    elif split_website[0] == 'meta':
        # eg: original website: meta.computergraphics.stackexchange.com
        categories = ['meta']
        if split_website[1] in special_websites:
            categories.extend(list(special_websites[split_website[1]]))
        else:
            categories.append(split_website[1])
        return categories
    else:
        return [split_website[0]]
