import { combineReducers } from 'redux'
import { routerReducer } from 'react-router-redux'
import { reducer as formReducer } from 'redux-form'

import { authenticationReducer as authentication, authorisationReducer as authorisation } from 'stroom-js'

import singleAnnotation from './singleAnnotation'
import history from './history'
import manageAnnotations from './manageAnnotations'
import statusValues from './statusValues'
import snackbarMessages from './snackbarMessages'

export default combineReducers({
    routing: routerReducer,
    singleAnnotation,
    history,
    manageAnnotations,
    statusValues,
    snackbarMessages,
    authentication,
    authorisation,
    config : (state= {}) => state,
    form: formReducer
})
