import React from 'react'
import { render } from 'react-dom'
import thunkMiddleware from 'redux-thunk'
import { createLogger } from 'redux-logger'
import { Provider } from 'react-redux'

import {
    createStore,
    applyMiddleware
} from 'redux'

import {
    BrowserRouter as Router,
    Route,
    Switch
} from 'react-router-dom'

import {blue600, amber900} from 'material-ui/styles/colors'
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import getMuiTheme from 'material-ui/styles/getMuiTheme'

import AnnotationHistoryPage from './components/annotationHistoryPage'
import SingleAnnotationPage from './components/singleAnnotationPage'
import ManageAnnotationsPage from './components/manageAnnotationsPage'
import NotFoundPage from './components/notFoundPage'

import reducer from './reducers'

import { fetchStatusValues } from './actions/fetchStatusValues'
import {
    setAllowNavigation,
    setAnnotationId,
    setIndexUuid
} from './actions/setupUi'

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

// Just needs to be done once for the whole app
store.dispatch(fetchStatusValues())

const ManageAnnotationsRoutedPage = ({ match }) => {
    store.dispatch(setIndexUuid(match.params.indexUuid));
    store.dispatch(setAllowNavigation(true));
    return <ManageAnnotationsPage />
}

const SingleAnnotationRoutedPageWithNav = ({ match }) => {
    store.dispatch(setIndexUuid(match.params.indexUuid));
    store.dispatch(setAnnotationId(match.params.annotationId));
    store.dispatch(setAllowNavigation(true));
    return <SingleAnnotationPage />
}

const SingleAnnotationRoutedPageWithoutNav = ({ match }) => {
    store.dispatch(setIndexUuid(match.params.indexUuid));
    store.dispatch(setAnnotationId(match.params.annotationId));
    store.dispatch(setAllowNavigation(false));
    return <SingleAnnotationPage />
}

const AnnotationHistoryRoutedPageWithNav = ({ match }) => {
    store.dispatch(setIndexUuid(match.params.indexUuid));
    store.dispatch(setAnnotationId(match.params.annotationId));
    store.dispatch(setAllowNavigation(true));
    return <AnnotationHistoryPage />
}

const AnnotationHistoryRoutedPageWithoutNav = ({ match }) => {
    store.dispatch(setIndexUuid(match.params.indexUuid));
    store.dispatch(setAnnotationId(match.params.annotationId));
    store.dispatch(setAllowNavigation(false));
    return <AnnotationHistoryPage />
}

render(
    <MuiThemeProvider muiTheme={theme}>
        <Provider store={store}>
            <Router>
                <Switch>
                    <Route exact={true} path="/single/" component={NotFoundPage} />
                    <Route exact={true} path="/singleWithNav/:indexUuid/:annotationId?" component={SingleAnnotationRoutedPageWithNav} />
                    <Route exact={true} path="/single/:indexUuid/:annotationId?" component={SingleAnnotationRoutedPageWithoutNav} />
                    <Route exact={true} path="/historyWithNav/:indexUuid/:annotationId?" component={AnnotationHistoryRoutedPageWithNav} />
                    <Route exact={true} path="/history/:indexUuid/:annotationId?" component={AnnotationHistoryRoutedPageWithoutNav} />
                    <Route exact={true} path="/:indexUuid" component={ManageAnnotationsRoutedPage} />
                    <Route path="*" component={NotFoundPage}/>
                </Switch>
            </Router>
        </Provider>
    </MuiThemeProvider>,
    document.getElementById('root')
)
