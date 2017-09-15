import React from 'react'
import { render } from 'react-dom'
import thunkMiddleware from 'redux-thunk'
import { createLogger } from 'redux-logger'
import { createStore, applyMiddleware } from 'redux'
import { Provider } from 'react-redux'
import App from './components/app'
import reducer from './reducers'
import { fetchAnnotation } from './actions/fetchAnnotation'
import { fetchStatusValues } from './actions/fetchStatusValues'

import {blue600, amber900} from 'material-ui/styles/colors'
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import getMuiTheme from 'material-ui/styles/getMuiTheme'

import {
  BrowserRouter as Router,
  Route
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
const Root = ({ match }) => {
    let app = undefined;

    if (!match.params.annotationId) {
        app = (
            <div>
                <p>No Annotation ID Specified</p>
            </div>
        )
    } else {
        store.dispatch(fetchAnnotation(match.params.annotationId))
        store.dispatch(fetchStatusValues())
        app = <App />
    }

    return (
        <MuiThemeProvider muiTheme={theme}>
            {app}
        </MuiThemeProvider>
    )
}

render(
  <Provider store={store}>
    <Router>
        <Route path="/:annotationId?" component={Root} />
    </Router>
  </Provider>,
  document.getElementById('root')
)
