import dash
import dash_table
import dash_html_components as html
import dash_core_components as dcc
from dash.dependencies import Input, Output, State
import flask
import pandas as pd
import plotly.graph_objs as go


df = pd.read_csv('C://Users//lucar//Desktop//Conteggi//Demo//Dati//Tw_gg.csv')
df2 = pd.read_csv('C://Users//lucar//Desktop//Conteggi//Demo//Dati//Username.csv')
df3 = pd.read_csv('C://Users//lucar//Desktop//Conteggi//Demo//Dati//Dettaglio.csv')

external_stylesheets = ['https://codepen.io/chriddyp/pen/bWLwgP.css']

app = dash.Dash(__name__, external_stylesheets=external_stylesheets)
app.config['suppress_callback_exceptions']=True  #If you are assigning callbacks to components that are generated by other callbacks (i miei grafici a torta)
                                                    #(and therefore not in the initial layout), then
                                                    #you can suppress this exception by setting
                                                    #`app.config['suppress_callback_exceptions']=True`.

#suddivisione pagina in tab
def serve_layout():

    # send initial layout if there is flask request context
    if flask.has_request_context():
        return layout_index

    # otherwise send every element to dash validator to prevent callback exceptions
    return html.Div([
        layout_index,
        layout_tab_1,
        layout_tab_2,
    ])

tabs_styles = {
    'height': '44px'
}
tab_style = {
    'borderBottom': '1px solid #d6d6d6',
    'padding': '6px',
    'fontWeight': 'bold'
}

tab_selected_style = {
    'borderTop': '1px solid #d6d6d6',
    'borderBottom': '1px solid #d6d6d6',
    'backgroundColor': '#119DFF',
    'color': 'white',
    'padding': '6px'
}

layout_index = html.Div([
    dcc.Tabs(id="tabs", value='tab-1', children=[
        dcc.Tab(label='Pagina', value='tab-1'),
        dcc.Tab(label='Pubblico', value='tab-2', children=[
            dcc.Tabs(id="subtabs", value="subtab1", children = [
                dcc.Tab(label='Panoramica', value="subtab1",style=tab_style, selected_style=tab_selected_style),
                dcc.Tab(label='Dettaglio', value="subtab2",style=tab_style, selected_style=tab_selected_style)
                        ])
        ]),
    ]),
    html.Div(id='tabs-content')])

#layout con grafico per i tweet
layout_tab_1 = html.Div([
    dash_table.DataTable(
        id='datatable-interactivity',
        columns=[
            {"name": i, "id": i, "deletable": True} for i in df.columns
        ],
        data=df.to_dict('records_Pagina'),
        editable=True,
        filtering=True,
        sorting=True,
        sorting_type="multi",
        row_selectable="multi",
        row_deletable=True,
        selected_rows=[],
        pagination_mode="fe",
        pagination_settings={
            "current_page": 0,
            "page_size": 5,
        },
    ),
    html.Div(id='datatable-interactivity-container')
])

layout_tab_2 = html.Div([
    dash_table.DataTable(
        id='datatable-interactivity-2',
        columns=[
            {"name": i, "id": i, "deletable": True} for i in df2.columns
        ],
        data=df2.to_dict('records_Pubblico'),
        editable=True,
        filtering=True,
        sorting=True,
        sorting_type="multi",
        row_selectable="multi",
        row_deletable=True,
        selected_rows=[],
        pagination_mode="fe",
        pagination_settings={
            "current_page": 0,
            "page_size": 5,
        },
    ),
    html.Div(id='datatable-interactivity-container-2'),
    
])

available_indicators = df3['Username'].unique()

layout_tab_3 = html.Div([
        html.Div([
            dcc.Dropdown(
             id='crossfilter-xaxis-column',
             options=[{'label': i, 'value': i} for i in available_indicators]),
             ], style={'verticalAlign': 'top', 'width': '10%', 'display': 'inline-block'}),


    html.Div([
        dcc.Graph(
                id='crossfilter-indicator-pie')
             ], style={'width': '30%', 'display': 'inline-block'}),
    
    html.Div([
        dcc.Graph(
                id='crossfilter-indicator-pie2')
             ], style={'width': '30%', 'display': 'inline-block'}),
    
    html.Div([
        dcc.Graph(
                id='crossfilter-indicator-pie3')
             ], style={'width': '30%', 'display': 'inline-block'}),
    
    #html.Div([
        #dcc.Graph(
               # id='graph_map',
               # figure=
               # )
           # ],className='six columns',
           #   style={'margin-top': '10'})
])

app.layout = serve_layout

# Index callbacks
@app.callback(Output('tabs-content', 'children'),
              [Input('tabs', 'value'),
               Input('subtabs', 'value')])
def render_content(tab,subtab):
    if tab == 'tab-1':
        return layout_tab_1
    elif tab == 'tab-2':
        if subtab == "subtab1":
            return layout_tab_2
        elif subtab == "subtab2":
            return layout_tab_3


# Tab 1 Callbacks
@app.callback(
    Output('datatable-interactivity-container', "children"),
    [Input('datatable-interactivity', "derived_virtual_data"),
     Input('datatable-interactivity', "derived_virtual_selected_rows")])
def update_graphs(rows, derived_virtual_selected_rows):
    # When the table is first rendered, `derived_virtual_data` and
    # `derived_virtual_selected_rows` will be `None`. This is due to an
    # idiosyncracy in Dash (unsupplied properties are always None and Dash
    # calls the dependent callbacks when the component is first rendered).
    # So, if `rows` is `None`, then the component was just rendered
    # and its value will be the same as the component's dataframe.
    # Instead of setting `None` in here, you could also set
    # `derived_virtual_data=df.to_rows('dict')` when you initialize
    # the component.
    if derived_virtual_selected_rows is None:
        derived_virtual_selected_rows = []

    dff = df if rows is None else pd.DataFrame(rows)
    
    colors = ['#7FDBFF' if i in derived_virtual_selected_rows else '#0074D9'
              for i in range(len(dff))]
    colorsNeg = ['#FF7F7F' if i in derived_virtual_selected_rows else 'EF0202'
              for i in range(len(dff))]
    colorsP = ['#7FFF9F' if i in derived_virtual_selected_rows else '#2ECC71'
              for i in range(len(dff))]
    colorsNeu = ['#C5C5C5' if i in derived_virtual_selected_rows else '#676767'
              for i in range(len(dff))]

    return [
        dcc.Graph(
            #id=column,
            figure={
                "data": [
                    {
                        "x": dff["Data"],
                        "y": dff["Totale"],
                        "type": "bar",
                        "marker": {"color": colors},
                    }
                ],
                "layout": {
                    "xaxis": {"automargin": True},
                    "yaxis": {
                        "automargin": True,
                        "title": {"text": "Tweet giornalieri"}
                    },
                    "height": 250,
                    "margin": {"t": 10, "l": 10, "r": 10},
                },
            },
        ),
        # check if column exists - user may have deleted it
        # If `column.deletable=False`, then you don't
        # need to do this check.
        #for column in ["Totale"] if column in dff

        dcc.Graph(
            #id=column,
            figure={
                "data": [
                    {
                        "x": dff["Data"],
                        "y": dff["negative"],
                        "type": "bar",
                        "marker": {"color": colorsNeg},
                        "name" : "Negativo"
                    },

                    {
                        "x": dff["Data"],
                        "y": dff["positive"],
                        "type": "bar",
                        "marker": {"color": colorsP},
                        "name" : "Positivo"
                    },

                    {
                        "x": dff["Data"],
                        "y": dff["neutral"],
                        "type": "bar",
                        "marker": {"color": colorsNeu},
                        "name" : "Neutrale"
                    }
                ],
                "layout": {
                    "xaxis": {"automargin": True},
                    "yaxis": {
                        "automargin": True,
                        "title": {"text": "Sentiment"}
                    },
                    "height": 250,
                    "margin": {"t": 10, "l": 10, "r": 10},
                },
            },
        )
    ]

# Tab 2 Callbacks
@app.callback(
    Output('datatable-interactivity-container-2', "children"),
    [Input('datatable-interactivity-2', "derived_virtual_data"),
     Input('datatable-interactivity-2', "derived_virtual_selected_rows")])
def update_graphs(rows2, derived_virtual_selected_rows2):
    # When the table is first rendered, `derived_virtual_data` and
    # `derived_virtual_selected_rows` will be `None`. This is due to an
    # idiosyncracy in Dash (unsupplied properties are always None and Dash
    # calls the dependent callbacks when the component is first rendered).
    # So, if `rows` is `None`, then the component was just rendered
    # and its value will be the same as the component's dataframe.
    # Instead of setting `None` in here, you could also set
    # `derived_virtual_data=df.to_rows('dict')` when you initialize
    # the component.
    if derived_virtual_selected_rows2 is None:
        derived_virtual_selected_rows2 = []

    dff2 = df2 if rows2 is None else pd.DataFrame(rows2)
    colors = ['#7FDBFF' if i in derived_virtual_selected_rows2 else '#0074D9'
              for i in range(len(dff2))]

    return [
        dcc.Graph(
            id=column,
            figure={
                "data": [
                    {
                        "x": dff2["Username"],
                        "y": dff2[column],
                        "type": "bar",
                        "marker": {"color": colors},
                    }
                ],
                "layout": {
                    "xaxis": {"automargin": True},
                    "yaxis": {
                        "automargin": True,
                        "title": {"text": column}
                    },
                    "height": 250,
                    "margin": {"t": 10, "l": 10, "r": 10},
                },
            },
        )
        # check if column exists - user may have deleted it
        # If `column.deletable=False`, then you don't
        # need to do this check.
        for column in ["Tweet","Follower"] if column in dff2
    ]
       
# Tab 3 Callbacks
@app.callback(
Output('crossfilter-indicator-pie', 'figure'),
[Input('crossfilter-xaxis-column', 'value')])

def display_content(user_name):
    dff3 = df3[df3['Username'] == user_name]
    labels = ['Tweet', "Tweet Altrui"]
    values= [int(dff3["TweetTot"]),int(dff3["Differenza"])]
    colors = ['#08A0E9', '07587F']
    piedata = go.Pie(labels=labels,values=values,marker=dict(colors=colors))
    return {
     "data":[piedata],
        "layout" : {'title' : 'Tweet totali','height': 300,'margin': {'l': 30, 'b': 30, 'r': 30, 't': 60}, "legend" : {'x': 0.77}, "x" : 0.2},
        
    }

@app.callback(
Output('crossfilter-indicator-pie2', 'figure'),
[Input('crossfilter-xaxis-column', 'value')])

def display_content(user_name):
    dff3 = df3[df3['Username'] == user_name]
    labels = ['Tweet', "ReTweet"]
    values= [int(dff3["Tweet"]),int(dff3["RT"])]
    colors = ['#08A0E9', '#FF5733']
    piedata = go.Pie(labels=labels,values=values,marker=dict(colors=colors)) #5EBDFC
    return {
     "data":[piedata],
        "layout" : {'title' : 'Tweet e ReTweet','height': 300,'margin': {'l': 30, 'b': 30, 'r': 30, 't': 60}, "legend" : {'x': 0.77}},
        
    }

@app.callback(
Output('crossfilter-indicator-pie3', 'figure'),
[Input('crossfilter-xaxis-column', 'value')])

def display_content(user_name):
    dff3 = df3[df3['Username'] == user_name]
    labels = ['Neutrale', "Positivo","Negativo"]
    values= [int(dff3["neutral"]),int(dff3["positive"]),int(dff3["negative"])]
    colors = ['#676767', '#2ECC71', '#EF0202']
    piedata = go.Pie(labels=labels,values=values,marker=dict(colors=colors))
    return {
     "data":[piedata],
        "layout" : {'title' : 'Sentiment','height': 300,'margin': {'l': 30, 'b': 30, 'r': 30, 't': 60}, "legend" : {'x': 0.77}},
        
    }
    
   
if __name__ == '__main__':
    app.run_server(debug=True)