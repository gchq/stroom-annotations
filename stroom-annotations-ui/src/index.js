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

const Root = ({ match }) => {
    store.dispatch(fetchAnnotation(match.params.annotationId))

    if (!match.params.annotationId) {
        return <NoIdSpecified />
    } else {
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
