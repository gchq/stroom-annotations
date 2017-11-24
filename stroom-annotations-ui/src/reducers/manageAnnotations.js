import {
    REQUEST_SEARCH_ANNOTATIONS,
    REQUEST_MORE_ANNOTATIONS,
    RECEIVE_SEARCH_ANNOTATIONS,
    RECEIVE_SEARCH_ANNOTATIONS_FAILED
} from '../actions/searchAnnotations'

import {
    RECEIVE_CREATE_ANNOTATION
} from '../actions/createAnnotation'

import {
    RECEIVE_REMOVE_ANNOTATION
} from '../actions/removeAnnotation'

const defaultState = {
    searchTerm: '',
    annotations: [],
    canRequestMore: false
}

const manageAnnotations = (
    state = defaultState,
    action
 ) => {
    switch(action.type) {
        case RECEIVE_REMOVE_ANNOTATION: {
            return Object.assign({}, state, {
                    annotations : state.annotations.filter(a => a.id !== action.id)
                })
        }
        case RECEIVE_CREATE_ANNOTATION: {
            return Object.assign({}, state, {
                annotations: [
                    action.annotation,
                    ...state.annotations
                ]
            })
        }
        case REQUEST_SEARCH_ANNOTATIONS: {
            return Object.assign({}, state, {
                searchTerm: action.searchTerm,
                annotations: [],
                canRequestMore: false
            })
        }
        case REQUEST_MORE_ANNOTATIONS: {
            return Object.assign({}, state, {
                canRequestMore: false
            })
        }
        case RECEIVE_SEARCH_ANNOTATIONS: {
            let annotations = undefined;
            if (action.append) {
                annotations = [
                    ...state.annotations,
                    ...action.annotations
                ]
            } else {
                annotations = action.annotations
            }

            return Object.assign({}, state, {
                annotations,
                canRequestMore: (action.annotations.length > 0)
            })
        }
        case RECEIVE_SEARCH_ANNOTATIONS_FAILED: {
            return Object.assign({}, state, {
                    annotations: [],
                    canRequestMore: false
                  })
        }
        default:
            return state;
    }

}

export default (state, action) => manageAnnotations(state, action)