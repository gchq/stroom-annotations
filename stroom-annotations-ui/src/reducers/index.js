import { combineReducers } from 'redux'

import singleAnnotation from './singleAnnotation'
import history from './history'
import manageAnnotations from './manageAnnotations'
import statusValues from './statusValues'
import snackbarMessages from './snackbarMessages'
import errorMessages from './errorMessages'
import apiCalls from './apiCalls'

const annotationsApp = combineReducers({
    singleAnnotation,
    history,
    manageAnnotations,
    statusValues,
    snackbarMessages,
    errorMessages,
    apiCalls
})

export default annotationsApp
