import { combineReducers } from 'redux'

import annotation from './annotation'
import annotationHistory from './annotationHistory'
import annotations from './annotations'
import statusValues from './statusValues'

const annotationsApp = combineReducers({
  annotation, annotationHistory, annotations, statusValues
})

export default annotationsApp
