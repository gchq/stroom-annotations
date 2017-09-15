import React from 'react'
import { render } from 'react-dom'
import thunkMiddleware from 'redux-thunk'
import { createLogger } from 'redux-logger'
import { createStore, applyMiddleware } from 'redux'
import { Provider } from 'react-redux'
import App from './components/App'
import NoIdSpecified from './components/NoIdSpecified';
import reducer from './reducers'
import { fetchAnnotation } from './actions/fetchAnnotation'
import { fetchStatusValues } from './actions/fetchStatusValues'
import {
  BrowserRouter as Router,
  Route
} from 'react-router-dom'

const loggerMiddleware = createLogger()

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
    if (!match.params.annotationId) {
        return <NoIdSpecified />
    } else {
        store.dispatch(fetchAnnotation(match.params.annotationId))
        store.dispatch(fetchStatusValues())
        return <App />
    }
}

render(
  <Provider store={store}>
    <Router>
        <Route path="/:annotationId?" component={Root} />
    </Router>
  </Provider>,
  document.getElementById('root')
)
