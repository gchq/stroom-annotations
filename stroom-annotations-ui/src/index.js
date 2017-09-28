import React from 'react'
import { render } from 'react-dom'
import thunkMiddleware from 'redux-thunk'
import { createLogger } from 'redux-logger'
import { createStore, applyMiddleware } from 'redux'
import { Provider } from 'react-redux'
import SingleAnnotation from './components/singleAnnotation'
import ManageAnnotations from './components/manageAnnotations'
import reducer from './reducers'

import {blue600, amber900} from 'material-ui/styles/colors'
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import getMuiTheme from 'material-ui/styles/getMuiTheme'

import {
    BrowserRouter as Router,
    Route,
    Switch
} from 'react-router-dom'

const loggerMiddleware = createLogger()

const theme = getMuiTheme({
    palette: {
    primary1Color: blue600,
    accent1Color: amber900,
    }
})

const store = createStore(
    reducer,
    applyMiddleware(
        thunkMiddleware, // lets us dispatch() functions
        loggerMiddleware // neat middleware that logs actions
    )
)

// If opened as a dialog, do not present navigational items in the header
const SingleAnnotationMuiDialog = ({ match }) => {
    return <SingleAnnotation annotationId={match.params.annotationId} isDialog={true} />
}

const SingleAnnotationMuiPage = ({ match }) => {
    return <SingleAnnotation annotationId={match.params.annotationId} isDialog={false} />
}

render(
    <MuiThemeProvider muiTheme={theme}>
        <Provider store={store}>
            <Router>
                <Switch>
                    <Route exact={true} path="/single/:annotationId?" component={SingleAnnotationMuiDialog} />
                    <Route exact={true} path="/singleEdit/:annotationId?" component={SingleAnnotationMuiPage} />
                    <Route exact={true} path="/" component={ManageAnnotations} />
                </Switch>
            </Router>
        </Provider>
    </MuiThemeProvider>,
    document.getElementById('root')
)
