import React from 'react';
import { shallow } from 'enzyme';

import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-15';

configure({ adapter: new Adapter() });

import SingleAnnotation from './singleAnnotation';

describe('SingleAnnotationPage', () => {
    it('renders without crashing', () => {
        const annotation = {
            id: '1',
            content: 'some content',
        }

        shallow((
                <SingleAnnotation
                    isDialog={true}
                    isClean={true}
                    createAnnotation={() => {}}
                    fetchAnnotation={() => {}}
                    fetchAnnotationHistory={() => {}}
                    annotationId={annotation.id}
                    annotation={annotation}
                    history={{}}

                />
            ));
    });
})

