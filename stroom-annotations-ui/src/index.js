import React from 'react'
import { render } from 'react-dom'
import thunkMiddleware from 'redux-thunk'
import { createLogger } from 'redux-logger'
import { createStore, applyMiddleware } from 'redux'
import { Provider } from 'react-redux'
import App from './components/app'
import ManageAnnotations from './components/manageAnnotations'
import reducer from './reducers'
import { fetchAnnotation } from './actions/fetchAnnotation'
import { fetchAnnotationHistory } from './actions/fetchAnnotationHistory'
import { searchAnnotations } from './actions/searchAnnotations'
import { fetchStatusValues } from './actions/fetchStatusValues'

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

// This component is primarily responsible for deciding if the annotation ID has been specified
// and dispatching the initial fetchAnnotation action.
const SingleAnnotationMui = (annotationId, isDialog) => {
    let app = undefined;

    if (!annotationId) {
        app = (
            <div>
                No Annotation ID Found
            </div>
        )
    } else {
        store.dispatch(fetchAnnotation(annotationId))
        store.dispatch(fetchAnnotationHistory(annotationId))
        store.dispatch(fetchStatusValues())
        app = <App isDialog={isDialog} />
    }

    return (
        <MuiThemeProvider muiTheme={theme}>
            {app}
        </MuiThemeProvider>
    )
}

// If opened as a dialog, do not present navigational items in the header
const SingleAnnotationMuiDialog = ({ match }) => {
    return SingleAnnotationMui(match.params.annotationId, true);
}

const SingleAnnotationMuiPage = ({ match }) => {
    return SingleAnnotationMui(match.params.annotationId, false);
}

const ManageAnnotationsMui = ({ match }) => {
    store.dispatch(searchAnnotations())

    return (
        <MuiThemeProvider muiTheme={theme}>
            <ManageAnnotations />
        </MuiThemeProvider>
    )
}

render(
  <Provider store={store}>
    <Router>
        <Switch>
            <Route exact={true} path="/single/:annotationId?" component={SingleAnnotationMuiDialog} />
            <Route exact={true} path="/single/edit/:annotationId?" component={SingleAnnotationMuiPage} />
            <Route exact={true} path="/" component={ManageAnnotationsMui} />
        </Switch>
    </Router>
  </Provider>,
  document.getElementById('root')
)
