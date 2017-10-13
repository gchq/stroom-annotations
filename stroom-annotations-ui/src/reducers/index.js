import { combineReducers } from 'redux'

import singleAnnotation from './singleAnnotation'
import history from './history'
import manageAnnotations from './manageAnnotations'
import statusValues from './statusValues'
import snackbarMessages from './snackbarMessages'
import errorMessages from './errorMessages'
import apiCalls from './apiCalls'
import ui from './ui'

const annotationsApp = combineReducers({
    singleAnnotation,
    history,
    manageAnnotations,
    statusValues,
    snackbarMessages,
    errorMessages,
    apiCalls,
    ui
})

export default annotationsApp
